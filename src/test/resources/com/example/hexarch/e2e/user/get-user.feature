Feature: Get User - E2E Test
  Como cliente de la API
  Quiero poder obtener información de usuarios existentes
  Para consultar sus datos

  Background:
    * url baseUrl
    * def randomUsername = 'user_' + java.lang.System.currentTimeMillis()
    * def randomEmail = randomUsername + '@test.com'

    # Crear un usuario para los tests
    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(randomUsername)",
        "email": "#(randomEmail)"
      }
      """
    When method POST
    Then status 201
    * def createdUserId = response.id
    * def createdUsername = response.username
    * def createdEmail = response.email
    * def createdAt = response.createdAt

  Scenario: Obtener un usuario existente por ID
    Given path '/api/v1/users/' + createdUserId
    When method GET
    Then status 200
    And match response.id == createdUserId
    And match response.username == createdUsername
    And match response.email == createdEmail
    And match response.createdAt == createdAt

  Scenario: Obtener un usuario inexistente debe devolver 404
    # Generar un UUID aleatorio que probablemente no existe
    * def nonExistentId = java.util.UUID.randomUUID().toString()
    Given path '/api/v1/users/' + nonExistentId
    When method GET
    Then status 404
    And match response.status == 404
    And match response.error == 'Not Found'
    And match response.message contains 'not found'

  Scenario: Obtener usuario con ID inválido debe devolver 400
    Given path '/api/v1/users/invalid-uuid-format'
    When method GET
    Then status 400

  Scenario: Flujo completo - Crear y obtener usuario
    # 1. Crear un nuevo usuario
    * def newUsername = 'flow_user_' + java.lang.System.currentTimeMillis()
    * def newEmail = newUsername + '@test.com'

    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(newUsername)",
        "email": "#(newEmail)"
      }
      """
    When method POST
    Then status 201
    * def newUserId = response.id

    # 2. Obtener el usuario recién creado
    Given path '/api/v1/users/' + newUserId
    When method GET
    Then status 200
    And match response.id == newUserId
    And match response.username == newUsername
    And match response.email == newEmail
    And match response.createdAt == '#present'

  Scenario: Verificar que diferentes usuarios tienen IDs únicos
    # Obtener el primer usuario (del Background)
    Given path '/api/v1/users/' + createdUserId
    When method GET
    Then status 200
    * def user1 = response

    # Crear un segundo usuario
    * def user2Username = 'unique_user_' + java.lang.System.currentTimeMillis()
    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(user2Username)",
        "email": "#(user2Username + '@test.com')"
      }
      """
    When method POST
    Then status 201
    * def user2Id = response.id

    # Obtener el segundo usuario
    Given path '/api/v1/users/' + user2Id
    When method GET
    Then status 200
    * def user2 = response

    # Verificar que son diferentes
    And match user1.id != user2.id
    And match user1.username != user2.username
    And match user1.email != user2.email
