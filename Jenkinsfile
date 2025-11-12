def configurations = [
  [ platform: "linux", jdk: 21 ],
  [ platform: "windows", jdk: 21 ]
]

def params = [
    failFast: false,
    configurations: configurations,
    checkstyle: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]],
    pmd: [qualityGates: [[threshold: 1, type: 'NEW', unstable: true]]],
    jacoco: [sourceCodeRetention: 'MODIFIED']
    ]

buildPlugin(params)
