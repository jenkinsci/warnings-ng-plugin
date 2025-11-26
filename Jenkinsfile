def configurations = [
  [ platform: "linux", jdk: 21 ],
  [ platform: "windows", jdk: 17 ]
]

def params = [
    failFast: false,
    timeout: 90,
    configurations: configurations,
    checkstyle: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]],
    pmd: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]],
    jacoco: [sourceCodeRetention: 'MODIFIED']
    ]

buildPlugin(params)
