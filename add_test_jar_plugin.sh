#!/bin/bash
FILE=$1
if ! grep -q "test-jar" "$FILE"; then
  # Insert maven-jar-plugin configuration before </plugins>
  sed -i '' '/<\/plugins>/i\
            <plugin>\
                <groupId>org.apache.maven.plugins<\/groupId>\
                <artifactId>maven-jar-plugin<\/artifactId>\
                <version>3.3.0<\/version>\
                <executions>\
                    <execution>\
                        <goals>\
                            <goal>test-jar<\/goal>\
                        <\/goals>\
                    <\/execution>\
                <\/executions>\
            <\/plugin>\
' "$FILE"
fi
