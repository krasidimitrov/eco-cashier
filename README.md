# eco-cashier
Simple management for money deposit and withdraw.

I first implemented the deposit and withdraw functionality. I did not have time to finish the
get balance functionality. But here is my thought process. I was thinking of using a different text file to store balances
for each day so in that way I would have easier time when getting for multiple dates the balances. The problem was that dates were without timestamp which would lead to some inconsistency simply because getting the balance for a date with a specific timezone may omit some records because everything was stored in UTC. The UTC was the correct way to store things but timestamp was also necessary in order to convert properly when we are presented with a specific timezone. 


In conclusion about this approach I should have gone with storing the balances in single file and then when asked for specific range of dates to fetch balances I would need to also fetch all transactions and sum them in order to get the appropriate balance for the specific dates. That would have been much easier if the requirement allowed the use of database.
But sadly no time to refactor. I would also introduce ControllerAdvice for error handling, add unit tests for all service logic and introduce server side objects that are transformed from and to requests objects in the controllers so the service logic can have separate objects.