# Lab Four - Library Alert System

## Part One: Test Design

### System Under Test

The method under test is:

`LibraryService.checkoutResource(UUID resourceId, String memberEmail)`

The method checks whether a library resource is available, updates its status when it is checked out, and sends an email notification to the member.

The `ResourceRepository` and `EmailProvider` are external dependencies and will be mocked during unit testing.

---

## Equivalence Partitions

| Input / Condition | Valid Partition | Invalid / Failure Partition |
|---|---|---|
| Resource ID | Non-null UUID | Null UUID |
| Resource Availability | Resource is available | Resource is unavailable |
| Status Update | Update succeeds | Update fails |
| Email Notification | Email succeeds | Email fails |

---

## Test Cases

| Test ID | Test Scenario | Conditions | Expected Result |
|---|---|---|---|
| TC-01 | Null Resource ID | Resource ID is null | Method returns false |
| TC-02 | Resource Unavailable | Resource ID is valid but resource is unavailable | Method returns false |
| TC-03 | Successful Checkout | Resource is available, status update succeeds, and email succeeds | Method returns true |
| TC-04 | Status Update Failure | Resource is available but status update fails | `DatabaseFailureException` is thrown |
| TC-05 | Email Failure | Resource is available, status update succeeds, but email fails | `EmailFailureException` is thrown |

---

## Expected Dependency Interactions

| Test ID | Repository Check | Status Update | Email Sent |
|---|---|---|---|
| TC-01 | No | No | No |
| TC-02 | Yes | No | No |
| TC-03 | Yes | Yes | Yes |
| TC-04 | Yes | Yes | No |
| TC-05 | Yes | Yes | Yes |

---

## Test Design Rationale

The test cases cover the major execution paths of the `checkoutResource` method. They include a null resource ID, an unavailable resource, a successful checkout, a database status update failure, and an email notification failure.

Equivalence partitioning is used to divide the important conditions into successful and unsuccessful cases. The tests also exercise the major branches of the `checkoutResource` method.

Mockito will be used to mock `ResourceRepository` and `EmailProvider`. This allows the behavior of `LibraryService` to be tested independently without accessing a real database or sending real emails.