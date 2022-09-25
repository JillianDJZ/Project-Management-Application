describe('Editing user account info', () => {

    beforeEach(() => {
        cy.adminLogin()
        cy.visit("/account")
        cy.request({
            method: 'POST',
            url: 'edit/details',
            form: true,
            body: {
                "firstname": "John",
                "middlename": "McSteves",
                "lastname": "Wayne",
                "nickname": "Stev",
                "bio": "Hello! my name is John and I am your course administrator!",
                "personalPronouns": "He/Him",
                "email": "steve@gmail.com"
            },
        })
    })

    it("resets account data on cancel edit", () => {
        cy.get(".editUserButton").click()
        cy.get("#firstname").invoke('val', "hello")
        cy.get("#middlename").invoke('val', "hello")
        cy.get("#lastname").invoke('val', "hello")
        cy.get("#nickname").invoke('val', "hello")
        cy.get("#email").invoke('val', "hello")
        cy.get("#bio").invoke('val', "hello")
        cy.get("#personalPronouns").invoke('val', "hello")
        cy.get(".editUserButton").click().wait(500)

        cy.get("#firstname").should('have.value','John')
        cy.get("#middlename").should('have.value','McSteves')
        cy.get("#lastname").should('have.value','Wayne')
        cy.get("#nickname").should('have.value','Stev')
        cy.get("#email").should('have.value','steve@gmail.com')
        cy.get("#bio").should('have.value','Hello! my name is John and I am your course administrator!')
        cy.get("#personalPronouns").should('have.value','He/Him')
    })

    it("Displays multiple errors without submission", () => {
        cy.get(".editUserButton").click()
        cy.get("#firstname").invoke('val', "99")
        cy.get("#middlename").invoke('val', "99")
        cy.get("#lastname").invoke('val', "99")
        cy.get("#nickname").invoke('val', "Ⅵ")
        cy.get("#email").invoke('val', "99")
        cy.get("#bio").type("Ⅵ")
        cy.get("#personalPronouns").invoke('val', "Ⅴ")

        cy.get("#firstname").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#middlename").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#lastname").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#nickname").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#email").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#bio").should("have.css", "border-color", 'rgb(220, 53, 69)');
        cy.get("#personalPronouns").should("have.css", "border-color", 'rgb(220, 53, 69)');

        cy.get("#firstNameError").should("be.visible")
        cy.get("#middleNameError").should("be.visible")
        cy.get("#lastNameError").should("be.visible")
        cy.get("#nickNameError").should("be.visible")
        cy.get("#emailError").should("be.visible")
        cy.get("#bioError").should("be.visible")
        cy.get("#pronounsError").should("be.visible")
    })
})