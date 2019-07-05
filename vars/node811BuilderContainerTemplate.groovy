def call() {
  return [
    containerTemplate(
      name: 'node811-builder',
      image: 'agiledigital/node811-builder',
      alwaysPullImage: true,
      command: 'cat',
      ttyEnabled: true
    )
  ]
}