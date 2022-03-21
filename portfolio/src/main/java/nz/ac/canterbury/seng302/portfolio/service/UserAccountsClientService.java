package nz.ac.canterbury.seng302.portfolio.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The UserAccountsClientServices class implements the functionality of the services outlined
 * by the user_accounts.proto gRPC contacts. This allows the client to make requests to the server
 * regarding their account.
 *
 * @author Sam Clark
 */
@Service
public class UserAccountsClientService {

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceStub nonBlockingStub;

    /**
     * Sends a request to the UserAccountsServerService containing the id of a user, requesting the users account details.
     *
     * @param request - The request to send to the server, uses the GetUserByIDRequest message type defined by user_accounts.proto
     * @return response - The servers response to the request, which follows the UserResponse message format.
     */
    public UserResponse getUserAccountById(GetUserByIdRequest request) {
        return userAccountStub.getUserAccountById(request);
    }

    /**
     * Sends a request to the UserAccountServerService to register a new user, with a UserRegisterRequest message
     *
     * @param request - The request for a registration, uses the UserRegisterRequest message type defined in the user_accounts.proto contract
     * @return response - A UserRegisterResponse with the information returned regarding the registration attempt.
     */
    public UserRegisterResponse register(UserRegisterRequest request) {
        return userAccountStub.register(request);
    }

    /**
     * Sends a request to the UserAccountServerService to edit the details of a user, with a EditUserRequest message
     *
     * @param request -The request for an edit, uses the EditUserRequest message type defined in the user_accounts.proto contract
     * @return response - A EditUserResponse with the information returned regarding the detail editing attempt.
     */
    public EditUserResponse editUser(EditUserRequest request) {
        return userAccountStub.editUser(request);
    }

    /**
     * Sends a request to the UserAccountServerService to change the password of a user, with a ChangePasswordRequest message
     *
     * @param request -The request to change password, uses the ChangePasswordRequest message type defined in the user_accounts.proto contract
     * @return response - A ChangePasswordResponse with the information returned regarding the changing of passwords.
     */
    public ChangePasswordResponse changeUserPassword(ChangePasswordRequest request) {
        return userAccountStub.changeUserPassword(request);
    }

    /**
     * Takes a list of request chunks built in the controller and sends them one by on to the server.
     * Note the first chunk should be metadata and the rest file data.
     *
     * @param requestChunks -  the List<UploadUserProfilePhotoRequest> containing the image data chunks.
     */
    public void uploadUserProfilePhoto(List<UploadUserProfilePhotoRequest> requestChunks) {

        ImageResponseStreamObserver responseObserver = new ImageResponseStreamObserver();
        StreamObserver<UploadUserProfilePhotoRequest> requestObserver = nonBlockingStub.uploadUserProfilePhoto(responseObserver);
        responseObserver.initialise(requestObserver);
        responseObserver.sendImage(requestChunks);
        // may want to return the results from the sendImage method.

    }
}
