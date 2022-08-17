describe('Test Registration', () => {
  it('Visits the local web address for registration', () => {
    cy.visit('/login')
    cy.contains("here").click()
    cy.url().should("include", "/register")
  })
  it('Fills in the registration details with correct information', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middleName").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#username").type("TestingUsername" + Math.floor(Math.random() * 100))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.url().should("include", '/account')
  })

  it('Fills in the registration details with bad username', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middleName").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('equal', 'Please fill out this field.')
  })
  it('Fills in the registration details with bad firstname', () => {
    cy.visit('/register')

    cy.get("#middleName").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#password").type("password")
    cy.get("#username").type("TestingUsername" + Math.floor(Math.random() * 100))
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#firstname:invalid')
        .invoke('prop', 'validationMessage')
        .should('equal', 'Please fill out this field.')
  })

  it('Fills in the registration details with bad lastname', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middleName").type("This is a test")
    cy.get("#password").type("password")
    cy.get("#username").type("TestingUsername" + Math.floor(Math.random() * 100))
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#lastname:invalid')
        .invoke('prop', 'validationMessage')
        .should('equal', 'Please fill out this field.')
  })

  it('Fills in the registration details with username emoji', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middleName").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#username").type("😀" + Math.floor(Math.random() * 10))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.contains("Submit").click()
    cy.get('#username:invalid')
        .invoke('prop', 'validationMessage')
        .should('include', 'Please match the requested format')
  })

  it('Fills in the registration details with bio emoji', () => {
    cy.visit('/register')
    cy.get("#firstname").type("This is a test")
    cy.get("#middleName").type("This is a test")
    cy.get("#lastname").type("This is a test")
    cy.get("#username").type("test" + Math.floor(Math.random() * 10))
    cy.get("#password").type("password")
    cy.get("#email").type("test@test.com")
    cy.get("#bio").type("😀 😃 😄"+ Math.floor(Math.random() * 10))
    cy.contains("Invalid character")
  })
})


describe('Test Login Page', () => {
  it('Visits the local web address for login ', () => {
    cy.visit('http://localhost:9000/')
    cy.contains("Sign In")
  })
  it('Tries to login with invalid user', () => {
    cy.get("#username").type("badUserName").should('have.value', "badUserName")
    cy.get("#password").type("thisIsNot").should('have.value', "thisIsNot")
    cy.get(".btn-primary").click()
    cy.contains("Log in attempt failed: could not find user badUserName")
  })
  it('Tries to login with valid user', () => {
    cy.get("#username").type("steve").should('have.value', "steve")
    cy.get("#password").type("password").should('have.value', "password")
    cy.contains("Submit").click()
    cy.url().should("include", "/account")
  })
  it('Contains date of registration', () => {
    cy.contains("Member since: ")
  })
  it('Menu contains logout', () => {
    cy.get(".profileDropdown").click().get(".logout").click()
    cy.url().should("include", "/login")
  })
})