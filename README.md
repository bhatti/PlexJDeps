# PlexJDeps - Dependency Graph

## Overview

Creates dot files for class or package dependencies and generates sequence diagrams for unit tests.

## Version
 - 0.1 : experimental
 
## License
 - MIT

## How To Guide
### Arguments
| Argument     | Description |
|--------------|-----------|
| -P | package-only        |
| -p <package> | package(s) to scan - you can specify multiple -p |
| -m <token> | token(s) that must be included - you can specify multiple -m |
| -k <token> | token(s) that must not be included - you can specify multiple -k |
| <list of class names> | |

### Generate Class Dependency
```
java  -cp lib-all.jar com.plexobject.deps.ShowDepend -p com.plexobject com.plexobject.demo.ddb.api.MusicAPI
```

### Generate Package Dependency
```
java  -cp lib-all.jar com.plexobject.deps.ShowDepend -P -p com.plexobject com.plexobject.demo.ddb.api.MusicAPI
```

### Generate Sequence Diagram
```
java -javaagent:aspectjweaver-1.9.9.jar -cp lib-all.jar com.plexobject.deps.ShowDepend -s com.plexobject.demo.ddb.api.MusicAPI
```

## Support or Contact
  Email bhatti AT plexobject DOT com for any questions or suggestions.

