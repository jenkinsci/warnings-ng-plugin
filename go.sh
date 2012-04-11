rm -rf $HUDSON_HOME/plugins/warnings*

mvn install || { echo "Build failed"; exit 1; }
 
cp -f target/*.hpi $HUDSON_HOME/plugins/

cd $HUDSON_HOME
./debug.sh
