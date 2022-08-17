package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.Authentication;
import nz.ac.canterbury.seng302.portfolio.service.grpc.UserAccountsClientService;
import nz.ac.canterbury.seng302.portfolio.model.domain.preferences.UserPrefRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import nz.ac.canterbury.seng302.shared.util.PaginationResponseOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;

import java.util.*;

import static org.mockito.Mockito.*;

@SpringBootTest
class UserListControllerTest {

    private static final UserAccountsClientService mockClientService = mock(UserAccountsClientService.class);

    @InjectMocks
    private static final UserListController userListController = spy(UserListController.class);
    private final ArrayList<UserResponse> expectedUsersList = new ArrayList<>();
    private final Authentication principal = new Authentication(AuthState.newBuilder().addClaims(ClaimDTO.newBuilder().setType("nameid").setValue("1").build()).build());

    @Autowired
    private UserPrefRepository userPrefRepository;

    /** used to set the values for the tests, this number should be the same as the value in the UserListController **/
    private int usersPerPage = 20;

    /** First Name Comparator, has other name fields after to decide order if first names are the same*/
    Comparator<UserResponse> compareByFirstName = Comparator.comparing((UserResponse user) ->
            (user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getLastName().toLowerCase(Locale.ROOT)));

    /** Middle Name Comparator, has other name fields after to decide order if middle names are the same */
    Comparator<UserResponse> compareByMiddleName = Comparator.comparing((UserResponse user) ->
            (user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getLastName().toLowerCase(Locale.ROOT)));

    /** Last Name Comparator, has other name fields after to decide order if last names are the same */
    Comparator<UserResponse> compareByLastName = Comparator.comparing((UserResponse user) ->
            (user.getLastName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getMiddleName().toLowerCase(Locale.ROOT)));

    /** Username Comparator */
    Comparator<UserResponse> compareByUsername = Comparator.comparing((UserResponse user) ->
            (user.getUsername().toLowerCase(Locale.ROOT)));

    /** Alias Comparator, has name fields afterwards to decide order if the aliases are the same */
    Comparator<UserResponse> compareByAlias = Comparator.comparing((UserResponse user) ->
            (user.getNickname().toLowerCase(Locale.ROOT) + ' ' +
                    user.getFirstName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getMiddleName().toLowerCase(Locale.ROOT) + ' ' +
                    user.getLastName().toLowerCase(Locale.ROOT)));

    /** UserRoles Comparator */
    Comparator<UserResponse> compareByRole = (userOne, userTwo) -> {
        String userOneRoles = userOne.getRolesValueList().toString();
        String userTwoRoles = userTwo.getRolesValueList().toString();
        return userOneRoles.compareTo(userTwoRoles);
    };

    private static final Model model = setMockModel();


    @BeforeEach
    void beforeAll() {
        expectedUsersList.clear();
        //userListController.setUserAccountsClientService(mockClientService);
        UserResponse.Builder user = UserResponse.newBuilder();
        user.setUsername("steve")
                .setFirstName("Steve")
                .setMiddleName("McSteve")
                .setLastName("Steveson")
                .setNickname("Stev")
                .setBio("kdsflkdjf")
                .setPersonalPronouns("Steve/Steve")
                .setEmail("steve@example.com")
                .setProfileImagePath("a");
        user.addRoles(UserRole.STUDENT);

        when(PrincipalAttributes.getUserFromPrincipal(principal.getAuthState(), mockClientService)).thenReturn(user.build());
        addUsersToExpectedList(0, usersPerPage * 4 + 1);
        userPrefRepository.deleteAll();
        userListController.setPrefRepository(userPrefRepository);
    }


    /**
     * adds dummy users from a lower bound to an upper bound in order to test with multiple pages
     *
     * @param min the minimum number, used to ensure that there is no repeats of usernames
     * @param max the maximum number of users to be used for testing
     */
    private void addUsersToExpectedList(int min, int max) {
        for (int i = min; i < max; i++) {
            UserResponse.Builder user = UserResponse.newBuilder();
            user.setUsername("steve" + i)
                    .setFirstName("Steve" + i)
                    .setMiddleName("McSteve" + i)
                    .setLastName("Steveson" + i)
                    .setNickname("Stev" + i)
                    .setBio("kdsflkdjf")
                    .setPersonalPronouns("Steve/Steve")
                    .setEmail("steve@example.com");
            user.addRoles(UserRole.STUDENT);
            expectedUsersList.add(user.build());
        }
    }

    /**
     * Creates a mock response for a specific users per page limit and for an offset. Mocks the server side service of
     * retrieving the users from the repository
     *
     * @param offset the offset of where to start getting users from in the list, used for paging
     */
    private void createMockResponse(int offset, String sortOrder, String isAscending) {
        boolean boolAscending = Objects.equals(isAscending, "true");
        PaginationRequestOptions options = PaginationRequestOptions.newBuilder()
                                                                   .setOrderBy(sortOrder)
                                                                   .setLimit(usersPerPage)
                                                                   .setOffset(offset)
                                                                   .setIsAscendingOrder(boolAscending)
                                                                   .build();
        GetPaginatedUsersRequest request = GetPaginatedUsersRequest.newBuilder()
                                                                   .setPaginationRequestOptions(options)
                                                                   .build();
        PaginatedUsersResponse.Builder response = PaginatedUsersResponse.newBuilder();

        switch (sortOrder) {
            case "roles" -> expectedUsersList.sort(compareByRole);
            case "username" -> expectedUsersList.sort(compareByUsername);
            case "aliases" -> expectedUsersList.sort(compareByAlias);
            case "middlename" -> expectedUsersList.sort(compareByMiddleName);
            case "lastname" -> expectedUsersList.sort(compareByLastName);
            default -> expectedUsersList.sort(compareByFirstName);
        }

        if (!boolAscending) {
            Collections.reverse(expectedUsersList);
        }

        for (int i = offset; ((i - offset) < usersPerPage) && (i < expectedUsersList.size()); i++) {
            response.addUsers(expectedUsersList.get(i));
        }

        PaginationResponseOptions responseOptions = PaginationResponseOptions.newBuilder()
                                                                             .setResultSetSize(expectedUsersList.size())
                                                                             .build();

        response.setPaginationResponseOptions(responseOptions);
        when(mockClientService.getPaginatedUsers(request)).thenReturn(response.build());
    }


    @Test
    void contextLoads() {
        Assertions.assertNotNull(userListController);
    }


    @Test
    void loadFirstPage() {
        createMockResponse(0, "firstname", "true");
        userListController.getUserList(principal, model, 1, "firstname", "true");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadLastPage() {
        createMockResponse(usersPerPage * 4, "firstname", "true");
        userListController.getUserList(principal, model, 5, "firstname", "true");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(usersPerPage * 4, usersPerPage * 4 + 1);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadThirdPage() {
        createMockResponse(usersPerPage * 2, "firstname", "true");
        userListController.getUserList(principal, model, 3, "firstname", "true");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(usersPerPage * 2, usersPerPage * 3);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 3;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadLastPagePlusOne() {
        createMockResponse(usersPerPage * 5, "firstname", "true"); //needed so controller can see the total pages amount
        createMockResponse(usersPerPage * 4, "firstname", "true");
        userListController.getUserList(principal, model, 6, "firstname", "true");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(usersPerPage * 4, usersPerPage * 4 + 1);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 5;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadZeroPageNumber() {
        createMockResponse(0, "firstname", "true");
        userListController.getUserList(principal, model, 0, "firstname", "true");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void loadNegativePageNumber() {
        createMockResponse(0, "firstname", "true");
        userListController.getUserList(principal, model, -1, "firstname", "true");
        Object totalPages = model.getAttribute("totalPages");
        Object currentPage = model.getAttribute("currentPage");
        Object totalItems = model.getAttribute("totalItems");
        List<UserResponse> user_list = userListController.getUserResponseList();
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        String sortOrder = userListController.getSortOrder();

        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);
        int expectedTotalPages = 5;
        int expectedCurrentPage = 1;
        int expectedTotalItems = expectedUsersList.size();
        int expectedSubsetSize = expectedSubsetOfUsers.size();
        String expectedSortOrder = "firstname";
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);


        Assertions.assertEquals(expectedTotalPages, totalPages);
        Assertions.assertEquals(expectedCurrentPage, currentPage);
        Assertions.assertEquals(expectedTotalItems, totalItems);
        Assertions.assertEquals(expectedSubsetSize, user_list.size());
        Assertions.assertEquals(expectedSubsetOfUsers.toString(), user_list.toString());
        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
        Assertions.assertEquals(expectedSortOrder, sortOrder);
    }


    @Test
    void footerNumberSequenceLessThanElevenPages() {
        createMockResponse(0, "firstname", "true");
        userListController.getUserList(principal, model, 1, null, null);
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        List<Integer> expectedFooterSequence = Arrays.asList(1, 2, 3, 4, 5);

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }


    @Test
    void footerNumberSequencePage10GreaterThan16Pages() {
        addUsersToExpectedList(usersPerPage  * 4 + 2, usersPerPage * 18);
        createMockResponse(usersPerPage * 9, "firstname", "true");
        userListController.getUserList(principal, model, 10, "firstname", "true");
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        ArrayList<Integer> expectedFooterSequence = new ArrayList<>();
        for (int i = 5; i <= 15; i++) {
            expectedFooterSequence.add(i);
        }

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }


    @Test
    void footerNumberSequencePage10LessThan16Pages() {
        addUsersToExpectedList(usersPerPage  * 4 + 2, usersPerPage * 13);
        createMockResponse(usersPerPage * 9, "firstname", "true");
        userListController.getUserList(principal, model, 10, "firstname", "true");
        ArrayList<Integer> footerSequence = userListController.getFooterSequence();
        ArrayList<Integer> expectedFooterSequence = new ArrayList<>();
        for (int i = 3; i <= 13; i++) {
            expectedFooterSequence.add(i);
        }

        Assertions.assertEquals(expectedFooterSequence.toString(), footerSequence.toString());
    }


    @Test
    void sortByFirstNameIncreasing() {
        createMockResponse(0, "firstname", "true");
        userListController.getUserList(principal, model, 1, "firstname", "true");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByFirstName);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByFirstNameDecreasing() {
        createMockResponse(0, "firstname", "false");
        userListController.getUserList(principal, model, 1, "firstname", "false");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByFirstName);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByMiddleNameIncreasing() {
        createMockResponse(0, "middlename", "true");
        userListController.getUserList(principal, model, 1, "middlename", "true");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByMiddleName);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByMiddleNameDecreasing() {
        createMockResponse(0, "middlename", "false");
        userListController.getUserList(principal, model, 1, "middlename", "false");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByMiddleName);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByLastNameIncreasing() {
        createMockResponse(0, "lastname", "true");
        userListController.getUserList(principal, model, 1, "lastname", "true");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByLastName);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByLastNameDecreasing() {
        createMockResponse(0, "lastname", "false");
        userListController.getUserList(principal, model, 1, "lastname", "false");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByLastName);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByUsernameIncreasing() {
        createMockResponse(0, "username", "true");
        userListController.getUserList(principal, model, 1, "username", "true");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByUsername);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByUsernameDecreasing() {
        createMockResponse(0, "username", "false");
        userListController.getUserList(principal, model, 1, "username", "false");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByUsername);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByAliasIncreasing() {
        createMockResponse(0, "aliases", "true");
        userListController.getUserList(principal, model, 1, "aliases", "true");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByAlias);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByAliasDecreasing() {
        createMockResponse(0, "aliases", "false");
        userListController.getUserList(principal, model, 1, "aliases", "false");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByAlias);
        Collections.reverse(expectedUsersList);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByRolesIncreasing() {
        createMockResponse(0, "roles", "true");
        userListController.getUserList(principal, model, 1, "roles", "true");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByRole);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortByRolesDecreasing() {
        createMockResponse(0, "roles", "false");
        userListController.getUserList(principal, model, 1, "roles", "false");
        List<UserResponse> user_list = userListController.getUserResponseList();
        expectedUsersList.sort(compareByRole);
        List<UserResponse> expectedSubsetOfUsers = expectedUsersList.subList(0, usersPerPage);

        Assertions.assertEquals(expectedSubsetOfUsers, user_list);
    }


    @Test
    void sortOrderDefaultToNameIncreasing() {
        createMockResponse(0, "firstname", "true");
        String expectedDefaultSortOrder = "firstname";
        userListController.getUserList(principal, model, 1, null, null);
        String sortOrder = userListController.getSortOrder();
        boolean isAscending = userListController.getIsAscending();

        Assertions.assertEquals(expectedDefaultSortOrder, sortOrder);
        Assertions.assertTrue(isAscending);
    }


    @Test
    void sortOrderPersistence() {
        String expectedPersistedSortOrder = "role";
        createMockResponse(0, expectedPersistedSortOrder, "false");
        userListController.getUserList(principal, model, 1, expectedPersistedSortOrder, "false");
        String sortOrder = userListController.getSortOrder();
        boolean isAscending = userListController.getIsAscending();
        userListController.getUserList(principal, model, 1, null, null);

        Assertions.assertEquals(expectedPersistedSortOrder, sortOrder);
        Assertions.assertFalse(isAscending);
    }


    @SuppressWarnings("NullableProblems")
    private static Model setMockModel() {
        return new Model() {

            Object totalPages;
            Object currentPage;
            Object totalItems;
            Object user_list;
            Object possibleRoles;
            Object isAscending;

            @Override
            public Model addAttribute(String attributeName, Object attributeValue) {
                switch (attributeName) {
                    case "totalPages" -> totalPages = attributeValue;
                    case "currentPage" -> currentPage = attributeValue;
                    case "totalItems" -> totalItems = attributeValue;
                    case "user_list" -> user_list = attributeValue;
                    case "possibleRoles" -> possibleRoles = attributeValue;
                    case "isAscending" -> isAscending = attributeValue;
                }
                return null;
            }

            @Override
            public Model addAttribute(Object attributeValue) {
                return null;
            }

            @Override
            public Model addAllAttributes(Collection<?> attributeValues) {
                return null;
            }

            @Override
            public Model addAllAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public Model mergeAttributes(Map<String, ?> attributes) {
                return null;
            }

            @Override
            public boolean containsAttribute(String attributeName) {
                return false;
            }

            @Override
            public Object getAttribute(String attributeName) {
                Object toReturn = null;
                switch (attributeName) {
                    case "totalPages" -> toReturn = totalPages;
                    case "currentPage" -> toReturn = currentPage;
                    case "totalItems" -> toReturn = totalItems;
                    case "user_list" -> toReturn = user_list;
                    case "possibleRoles" -> toReturn = possibleRoles;
                    case "isAscending" -> toReturn = isAscending;
                }
                return toReturn;
            }

            @Override
            public Map<String, Object> asMap() {
                return null;
            }
        };
    }
}
