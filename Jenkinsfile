def configurations = [
  [ platform: "docker", jdk: "11" ],
  [ platform: "windows", jdk: "11" ]
]

buildPlugin(failFast: false, configurations: configurations, timeout: 90,
    checkstyle: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]],
    pmd: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]] )
