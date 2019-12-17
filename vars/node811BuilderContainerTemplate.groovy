def call() {
  return [
    containerTemplate(
      name: 'node811-builder',
      image: 'cypress/base',
      alwaysPullImage: true,
      command: 'cat',
      ttyEnabled: true,
    )
  ]
}
