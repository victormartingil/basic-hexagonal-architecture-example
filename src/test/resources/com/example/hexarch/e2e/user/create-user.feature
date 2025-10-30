Feature: Create User - E2E Test
  Como cliente de la API
  Quiero poder crear nuevos usuarios
  Para que el sistema los almacene correctamente

  Background:
    * url baseUrl
    * def randomUsername = 'user_' + java.lang.System.currentTimeMillis()
    * def randomEmail = randomUsername + '@test.com'

  Scenario: Crear un usuario exitosamente
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
    And match response.id == '#uuid'
    And match response.username == randomUsername
    And match response.email == randomEmail
    And match response.createdAt == '#present'

  Scenario: Crear un usuario sin username debe fallar
    Given path '/api/v1/users'
    And request
      """
      {
        "email": "#(randomEmail)"
      }
      """
    When method POST
    Then status 400
    And match response.status == 400
    And match response.error == 'Validation Error'

  Scenario: Crear un usuario sin email debe fallar
    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(randomUsername)"
      }
      """
    When method POST
    Then status 400
    And match response.status == 400
    And match response.error == 'Validation Error'

  Scenario: Crear un usuario con email inválido debe fallar
    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(randomUsername)",
        "email": "invalid-email-format"
      }
      """
    When method POST
    Then status 400
    And match response.status == 400
    And match response.error == 'Validation Error'

  Scenario: Crear múltiples usuarios y verificar que son diferentes
    # Crear primer usuario
    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(randomUsername + '_1')",
        "email": "#(randomUsername + '_1@test.com')"
      }
      """
    When method POST
    Then status 201
    And def firstUserId = response.id
    And def firstUsername = response.username

    # Crear segundo usuario
    Given path '/api/v1/users'
    And request
      """
      {
        "username": "#(randomUsername + '_2')",
        "email": "#(randomUsername + '_2@test.com')"
      }
      """
    When method POST
    Then status 201
    And def secondUserId = response.id
    And def secondUsername = response.username

    # Verificar que son diferentes
    And match firstUserId != secondUserId
    And match firstUsername != secondUsername
