def buildLog = new File(basedir, 'build.log').text
assert !buildLog.contains('this message should not appear when the test passes') :
    "Expected log message to be suppressed but found it in build output.\n" +
    "Build log:\n${buildLog}"
