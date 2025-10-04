# Birdsenger

This app lets you chat with your spouse, family, and friends.

---

#### 📢 Notice

This app by default uses external server to send and receive message. If server is run locally it must run first before the start of the application.
This project requires

- openjdk 25.0.1 (java 25)
- Gradle 9.1.0

```
public class SocketClient {
//    private static final String SERVER_HOST = "localhost";
      private static final String SERVER_HOST = "18.142.245.55";
      private static final int SERVER_PORT = 8080;
```

as you can see in the SocketClinet class in the Network folder in Birdsenger.
If you do want to run it locally Just comment it out and uncomment the localhost l
ine and if you do that you've start the server locally. The server
and application need to be started seperately.

To start the server open the terminal and move to the birdsenger folder and run

```
./gradlew runServer
```

---

## How to use this app

Clone this repo

```
git clone https://github.com/anime-in-the-world/BSG_Final.git
```

Get inside the directory at the loacation you cloned.

Now to try it out. Open 2 terminal side by side and get inside the root of the birdsenger folder.
Now to run it, use the following command and you must use it instead of regular gradlew run command

❌ Don't use it.

```
./gradlew run
```

✅ Use this

```
./gradlew --refresh-dependencies clean run --configuration-cache
```

Why are we doing this instead? Because it rebuilds the entire app without cache so you don't see any conflicts
when you're trying to log in. Otherwise you'll have login and connection issues.
