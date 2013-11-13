![Jukito] (http://i.imgur.com/sRnKp.png "Jukito") Jukito
The combined power of JUnit, Guice and Mockito. Plus it sounds like a cool martial art.

-----

So you started using dependency injection because somebody told you it would make your tests simpler? But as you gaze at your deep hierarchy of test classes, "simple" is not exactly the word you think of. Plus, creating a new mock whenever you add a parameter to an injected constructor gets old very quickly.

You are not alone! And Jukito was created specifically for people like you. Read on, or [get started](Getting-started) right away!

If you use [Google Guice](http://code.google.com/p/google-guice/), or if your GWT application uses [Gin](http://code.google.com/p/google-gin/), then Jukito is the perfect antidote to your unit testing headaches. Now you can write tests like this:

```java
@RunWith(JukitoRunner.class)
public class EmailSystemTest {

  @Inject EmailSystemImpl emailSystem;
  Email dummyEmail;

  @Before
  public void setupMocks(
      IncomingEmails incomingEmails,
      EmailFactory factory) {
    dummyEmail = factory.createDummy();
    when(incomingEmails.count()).thenReturn(1);
    when(incomingEmails.get(0)).thenReturn(dummyEmail);
  }

  @Test
  public void shouldFetchEmailWhenStarting(
      EmailView emailView) {
    // WHEN
    emailSystem.start();

    // THEN
    verify(emailView).addEmail(dummyEmail);
  }
}
```

That's right, Jukito lets you @Inject fields exactly as if your test class was injected with Guice. You can also inject parameters into your @Test, @Before and @After methods. Guice's just-in-time binding automatically instantiate your concrete classes, like EmailFactory. What about interfaces like IncomingEmails or EmailView? Jukito mocks them out automatically for you using mockito!

##Moving From
* https://code.google.com/p/jukito/ - Previous Jukito home.

##Reference
* [Jukito Custom Google Search](http://www.google.com/cse/home?cx=011138278718949652927:turyqq9pl64) - Search GWTP documentation, wiki and thread collections.
* [Jukito WebSite Source](https://github.com/ArcBees/jukito-website) - Jukito website source.
* [Jukito Google Group](https://groups.google.com/forum/?fromgroups#!forum/jukito) - Find help here.
* [Jukito Previous Repo](https://code.google.com/p/jukito/) - Previous home of Jukito.
* [GWTP](https://github.com/ArcBees/GWTP) - Find out more about the GWT-Platform.

##Latest Release
* 1.2

##Maven
* [Maven Central Jars](http://search.maven.org/#search%7Cga%7C1%7Corg.jukito) - Download jars from Maven Central.

  ```xml
  <dependency>
        <groupId>org.jukito</groupId>
        <artifactId>jukito</artifactId>
        <version>1.2</version>
        <scope>test</scope>
  </dependency>
  ```

##Javadocs
* [Javadocs](http://arcbees.github.com/Jukito/javadoc/apidocs/index.html)

##Thanks to
[![Arcbees.com](http://arcbees-ads.appspot.com/ad.png)](http://arcbees.com)

[![IntelliJ](https://lh6.googleusercontent.com/--QIIJfKrjSk/UJJ6X-UohII/AAAAAAAAAVM/cOW7EjnH778/s800/banner_IDEA.png)](http://www.jetbrains.com/idea/index.html)

[![githalytics.com alpha](https://cruel-carlota.gopagoda.com/df7cd27e91474db1118bebbddbeaa3ad "githalytics.com")](http://githalytics.com/ArcBees/Jukito)
