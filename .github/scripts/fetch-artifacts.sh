#!/usr/bin/env bash
set -euo pipefail

# This script expects the following environment variables:
# REPO: owner/repo
# HEAD_SHA: the commit SHA for which the producer workflows ran
# TOKEN: a token with read access to Actions (GITHUB_TOKEN or a PAT)
# OTHER_WORKFLOWS: comma-separated list of workflow filenames or workflow IDs
# ARTIFACT_NAMES: comma-separated list of artifact names (in the same order as WORKFLOWS)
# Optional:
# RETRIES: number of polling attempts per workflow (default: 30)
# SLEEP_SEC: seconds to wait between attempts (default: 10)

IFS=',' read -r -a WORKFLOWS_ARR <<< "${OTHER_WORKFLOWS}"
IFS=',' read -r -a ARTIFACTS_ARR <<< "${ARTIFACT_NAMES}"
REPO="${REPO}"
SHA="${HEAD_SHA}"
TOKEN="${TOKEN}"
API_BASE="https://api.github.com/repos/${REPO}"
RETRIES=${RETRIES:-30}
SLEEP_SEC=${SLEEP_SEC:-10}

mkdir -p artifacts

if [ ${#WORKFLOWS_ARR[@]} -ne ${#ARTIFACTS_ARR[@]} ]; then
  echo "ERROR: OTHER_WORKFLOWS and ARTIFACT_NAMES must have the same number of items"
  exit 1
fi

for idx in "${!WORKFLOWS_ARR[@]}"; do
  wf_raw="${WORKFLOWS_ARR[$idx]}"
  wf=$(echo "$wf_raw" | tr -d '[:space:]')
  art_raw="${ARTIFACTS_ARR[$idx]}"
  art=$(echo "$art_raw" | tr -d '[:space:]')

  echo "-- Waiting for workflow '$wf' to succeed for sha $SHA (artifact: $art)"
  attempt=0

  while true; do
    attempt=$((attempt+1))
    echo "  attempt $attempt/$RETRIES"

    # 1) Liste Runs für das Workflow (workflow id/name/file)
    resp=$(curl -s -H "Authorization: Bearer ${TOKEN}" "${API_BASE}/actions/workflows/${wf}/runs?per_page=50")

    # 2) Finde Run mit matching head_sha
    run_id=$(echo "$resp" | jq -r --arg SHA "$SHA" '.workflow_runs[] | select(.head_sha==$SHA) | .id' | head -n1 || true)
    run_status=$(echo "$resp" | jq -r --arg SHA "$SHA" '.workflow_runs[] | select(.head_sha==$SHA) | .status' | head -n1 || true)
    run_conclusion=$(echo "$resp" | jq -r --arg SHA "$SHA" '.workflow_runs[] | select(.head_sha==$SHA) | .conclusion' | head -n1 || true)

    if [ -n "${run_id}" ] && [ "${run_id}" != "null" ]; then
      echo "  Found run ${run_id} status=${run_status} conclusion=${run_conclusion}"

      if [ "${run_status}" = "completed" ]; then
        if [ "${run_conclusion}" = "success" ]; then
          echo "  Run succeeded — listing artifacts"
          art_resp=$(curl -s -H "Authorization: Bearer ${TOKEN}" "${API_BASE}/actions/runs/${run_id}/artifacts")
          art_id=$(echo "$art_resp" | jq -r --arg NAME "$art" '.artifacts[] | select(.name==$NAME) | .id' | head -n1 || true)

          if [ -n "${art_id}" ] && [ "${art_id}" != "null" ]; then
            echo "  Downloading artifact '$art' (id=${art_id})"
            out_zip="artifacts/${art}.zip"
            curl -L -s -H "Authorization: Bearer ${TOKEN}" "${API_BASE}/actions/artifacts/${art_id}/zip" -o "${out_zip}"
            echo "  Unpacking to artifacts/${art}/"
            mkdir -p "artifacts/${art}"
            unzip -o "${out_zip}" -d "artifacts/${art}" >/dev/null
            rm -f "${out_zip}"
            echo "  Artifact $art downloaded and unpacked"
            break
          else
            echo "  ERROR: Artifact '$art' not found in run ${run_id}"
            exit 1
          fi
        else
          echo "  ERROR: Run completed but conclusion='${run_conclusion}'"
          exit 1
        fi
      else
        echo "  Run exists but not completed yet (status=${run_status}), will retry"
      fi
    else
      echo "  No run found for workflow '${wf}' and sha ${SHA} yet"
    fi

    if [ ${attempt} -ge ${RETRIES} ]; then
      echo "ERROR: Timeout waiting for workflow '${wf}' to succeed for sha ${SHA}"
      exit 1
    fi

    sleep ${SLEEP_SEC}
  done

done

echo "All requested artifacts downloaded into ./artifacts/"
exit 0
