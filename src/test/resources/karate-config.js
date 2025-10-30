function fn() {
  // Configuración base de Karate
  var config = {
    // Headers comunes para todas las requests
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  };

  // Configuración según el entorno (profile)
  var env = karate.env; // get system property 'karate.env'

  karate.log('karate.env system property was:', env);

  if (!env) {
    env = 'local'; // default si no se especifica
  }

  // Configuración por entorno
  if (env === 'local') {
    // Tests contra aplicación corriendo en localhost
    config.baseUrl = 'http://localhost:8080';
    karate.log('Running E2E tests against LOCAL environment:', config.baseUrl);
  } else if (env === 'docker') {
    // Tests contra Docker Compose stack
    // Ajusta el host según tu configuración de Docker
    config.baseUrl = 'http://localhost:8080';
    karate.log('Running E2E tests against DOCKER environment:', config.baseUrl);
  } else if (env === 'ci') {
    // Tests en CI/CD pipeline
    config.baseUrl = 'http://localhost:8080';
    karate.log('Running E2E tests in CI environment:', config.baseUrl);
  }

  // Configuración de timeouts
  karate.configure('connectTimeout', 10000); // 10 segundos
  karate.configure('readTimeout', 10000);    // 10 segundos

  // Log level (puede ser 'info', 'debug', 'warn', 'error')
  karate.configure('logPrettyRequest', true);
  karate.configure('logPrettyResponse', true);

  return config;
}
