# Hinemos Manager Project

Hinemos Manager source forks from  &lt;https://osdn.net/projects/hinemos/>.

This branch, **grable**, is going to convert this existing project to build using gradle.

First, you have to manually get some jars in order to build. 
Follow the guide in lib/.readme and I believe it won't be too difficult. 

```
> gradle build
```

Then,
```
> gradle export
```

After all you can replace the original file in the Hinemos lib directory with the newly genearted build/lib/\*.jar

