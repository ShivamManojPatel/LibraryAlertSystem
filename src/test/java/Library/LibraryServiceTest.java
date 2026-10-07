package Library;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class LibraryServiceTest {
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private EmailProvider emailProvider;
    private LibraryService libraryService;

    @BeforeEach
    void setUp() {
        libraryService = new LibraryService(emailProvider, resourceRepository);
    }

    @Test
    void checkoutResource_nullResourceId_returnsFalse() throws DatabaseFailureException {
        boolean result = libraryService.checkoutResource(
                null,
                "member@gmail.com"
        );

        assertFalse(result);

        verifyNoInteractions(resourceRepository);
        verifyNoInteractions(emailProvider);
    }

    @Test
    void checkoutResource_resourceUnavailable_returnsFalse() throws DatabaseFailureException {
        UUID resourceId = UUID.randomUUID();

        when(resourceRepository.isResourceAvailable(resourceId)).thenReturn(false);

        boolean result = libraryService.checkoutResource(
                resourceId,
                "member@gmail.com"
        );

        assertFalse(result);

        verify(resourceRepository).isResourceAvailable(resourceId);
        verifyNoInteractions(emailProvider);
    }

    @Test
    void checkoutResource_successfulCheckout_returnsTrue() throws DatabaseFailureException {
        UUID resourceId = UUID.randomUUID();
        String memberEmail = "member@gmail.com";
        String expectedMessage = "Resource ID: " + resourceId + " checked out.";

        when(resourceRepository.isResourceAvailable(resourceId)).thenReturn(true);

        when(resourceRepository.updateStatus(resourceId, false)).thenReturn(true);

        when(emailProvider.sendEmail(memberEmail, expectedMessage)).thenReturn(true);

        boolean result = libraryService.checkoutResource(
                resourceId,
                memberEmail
        );

        assertTrue(result);

        verify(resourceRepository).isResourceAvailable(resourceId);
        verify(resourceRepository).updateStatus(resourceId, false);
        verify(emailProvider).sendEmail(memberEmail, expectedMessage);
    }

    @Test
    void checkoutResource_statusUpdateFails_throwsDatabaseFailureException() {
        UUID resourceId = UUID.randomUUID();
        String memberEmail = "member@gmail.com";

        when(resourceRepository.isResourceAvailable(resourceId)).thenReturn(true);

        try {
            when(resourceRepository.updateStatus(resourceId, false)).thenReturn(false);
        } catch (DatabaseFailureException e) {
            throw new RuntimeException(e);
        }

        assertThrows(
                DatabaseFailureException.class,
                () -> libraryService.checkoutResource(resourceId, memberEmail)
        );

        verify(resourceRepository).isResourceAvailable(resourceId);
        try{
            verify(resourceRepository).updateStatus(resourceId, false);
        } catch (DatabaseFailureException e) {
            throw new RuntimeException(e);
        }
        verify(emailProvider, never()).sendEmail(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void checkoutResource_emailFails_throwsEmailFailureException() throws DatabaseFailureException {
        UUID resourceId = UUID.randomUUID();
        String memberEmail = "member@gmail.com";
        String expectedMessage = "Resource ID: " + resourceId + " checked out.";

        when(resourceRepository.isResourceAvailable(resourceId)).thenReturn(true);

        when(resourceRepository.updateStatus(resourceId, false)).thenReturn(true);

        when(emailProvider.sendEmail(memberEmail, expectedMessage)).thenReturn(false);

        assertThrows(
                EmailFailureException.class,
                () -> libraryService.checkoutResource(resourceId, memberEmail)
        );

        verify(resourceRepository).isResourceAvailable(resourceId);
        verify(resourceRepository).updateStatus(resourceId, false);
        verify(emailProvider).sendEmail(memberEmail, expectedMessage);
    }
}
