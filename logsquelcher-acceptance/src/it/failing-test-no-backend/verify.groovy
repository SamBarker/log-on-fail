def buildLog = new File(basedir, 'build.log').text
assert buildLog.contains('this message should appear even without a logging backend') :
    "Expected replayed log message in build output but did not find it.\n" +
    "Build log:\n${buildLog}"
