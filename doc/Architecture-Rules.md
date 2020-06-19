Alle Klassen, die 
    - `@JavaScriptMethod` Methoden haben, müssen `public` sein
    - `@DataBoundConstructor` Konstruktor, haben, müssen `public` sein
    - die mit `@Extension` annotiert sind, haben, müssen `public` sein
    
Alle Konstuktoren, die 
    - mit `@DataBoundConstructor` annotiert sind, müssen `public` sein

Alle Methoden, die 
    - mit `@JavaScriptMethod` annotiert sind, müssen `public` sein
    - mit `@DataBoundSetter` annotiert sind, müssen `public` sein
    - mit `@DataBoundSetter` annotiert sind, benötigen einen passenden `public` getter 

Keine Methode darf die Annotation @CheckForNull bei Parametern nutzen 
(siehe NO_FORBIDDEN_ANNOTATION_USED und Workaround in https://github.com/TNG/ArchUnit/issues/373)

Alle Testklassen, die mit `Test` enden
    - dürfen nur JUNit 5 Elemente nutzen
    - dürfen max package private Methoden haben (mit @org.junit.Test)
    - alle package private Methoden müssen korrekt annotiert sein (also Testcase oder Disabled)

Alle Testklassen, die mit `ITest` enden
    - dürfen nur JUNit 4 Elemente nutzen
    - müssen public Methoden haben (mit @org.junit.Test)
    - alle public Methoden müssen korrekt annotiert sein (also Testcase oder Ignored)
    
Alle Aufrufe von `throw new ExceptionType` müssen einen Parameter haben (Throwable, String oder beides)

Alle Klassen, die eine `Object readResolve()` Methode überschreiben, müssen Serializable sein. Eine 
`Object readResolve()` Methode muss immer protected sein.
