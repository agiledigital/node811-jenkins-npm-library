def call(Map config) {
  return [
    [
      path: '/root/.npm',
      claimName: "${config.project}-home-jenkins-npm",
      sizeGiB: 1
    ],
    [
      path: '/root/.cache',
      claimName: "${config.project}-home-jenkins-cache",
      sizeGiB: 1
    ]
  ]
}