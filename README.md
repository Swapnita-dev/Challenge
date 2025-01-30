This project contains account details. It performs Account Transfer between accounts.

Main Changes done for Account transfer :

AccountsController : Added path as /transfer for Post request which is calling AccountsService's transfer method.

AcountsService : Consist of transfer method which is making transfer from one Account to another account with positive amount and having positive balance at the end in transferring account.
        Finally Notifications are sent via NotificationService. This account transfer is thread safe.
        
ChallengeApplicationTests : Consist of 4 tests done for Success Transfer positive case, 2 negative cases for negative balance and negative amount, one concurrency Test.        
