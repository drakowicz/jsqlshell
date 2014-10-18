#/bin/sh

cp="."
for jar in `ls *.jar`; do
  cp=$cp:$jar
done

java  -cp "$cp" JSQLShell $1
