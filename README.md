# node811-jenkins-npm-library
Toolform-compatible Jenkins 2 Pipeline build step for docker based builders using npm

This builder requires [Lerna](https://github.com/lerna/lerna) project as Monorepo, it doest `npm run bootstrap` to install all dependencies for all projects under `packages` folder.
