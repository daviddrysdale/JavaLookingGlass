JavaLookingGlass Android Application
====================================

This is an Android application that allows exploration of the available 
system Java libraries, using Java's reflection capabilities. 

You would think this wouldn't be that useful, given that the 
[Android documentation](http://developer.android.com/reference/packages.html) 
describes the classes available for developers to use.

However...

It turns out that Android ships with various other Java libraries, which are 
not advertised and aren't officially supported. 

It also [turns out](http://cacheinvalidation.blogspot.com/2011/08/android-java-classloading-gotcha.html) 
that the Dalvik classloader will prefer these system-installed libraries to any
versions of the same libraries that come as part of an application's APK file.

So it's useful to discover what libraries are present in the Android system, waiting like
a depth charge to [sabotage](http://code.google.com/p/libphonenumber/issues/detail?id=47) your app...

*Note*: The app includes wrappers for
 [java.lang.Package](http://download.oracle.com/javase/1.4.2/docs/api/java/lang/Package.html)
 information, but this doesn't appear to work &ndash; `Package.getPackages()` returns nothing, and
 `Package.getPackage(name)` always returns a Package object with no information in it, even for non-existent
 package names.
