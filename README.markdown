java-c2dm is a Java client for Google Cloud to Device Messaging (C2DM)
The library aims to provide a highly scalable interface to the Apple
server, while still being simple and modular.

The interface aims to require very minimal code to achieve the most common
cases, but have it be reconfigurable so you can even use your own networking
connections or XML library if necessary.

Links: [Installation](http://wiki.github.com/notnoop/java-c2dm/installation)
- [Javadocs](http://notnoop.github.com/java-c2dm/apidocs/index.html)
- [Changelog](https://github.com/notnoop/java-c2dm/blob/master/CHANGELOG)

Features:
--------------
  *  Easy to use, high performance C2DM Service API
  *  Easy to extend and reuse
  *  Easy to integrate with dependency injection frameworks
  *  Easy to setup custom notification payloads
  *  Supports connection pooling
  *  Supports message delegates and callbacks


Sample Code
----------------

To send a notification, you can do it in two steps:

1. Setup the connection

        C2DMService service =
            C2DM.newService()
            .authToken("serviceAuthenticationToken")
            .build();

2. Create and send the message

        C2DMNotification notification = C2dM.newNotification()
            .collapseKey("daily_message").delayWhileIdle()
            .build();
        String registrationId = "deviceRegistrationID"
        service.push(registrationId, notification);

That's it!

Features In the Making
---------------------------
  * Auto retries (exponential back-off feature)
  * More testing!

Sponsorship
---------------

This work is sponsored by [Excitor A/S](http://www.excitor.com/).

Contact
---------------
Support mailing list: http://groups.google.com/group/java-apns-discuss
