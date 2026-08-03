def buildLog = new File(basedir, 'build.log').text
assert buildLog.contains('live log from a passing test in realtime mode') :
    "Expected live log message in build output but did not find it.\n" +
    "Build log:\n${buildLog}"
