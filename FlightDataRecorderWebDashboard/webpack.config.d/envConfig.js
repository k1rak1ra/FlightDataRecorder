module.exports = env => {
    const webpack = require("webpack");

    var level;

    if (env.envTarget === "PROD") {
        level = "INFO";
    } else {
        level = "INFO";
    }

    const environmentPlugin = new webpack.EnvironmentPlugin(
    {
        KTOR_LOG_LEVEL: level,
    })

    config.plugins.push(environmentPlugin)

    return config
};