const { getDefaultConfig, mergeConfig } = require('expo/metro-config');
const { withNativeWind } = require('nativewind/metro');

const config = getDefaultConfig(__dirname);
const configWithNativeWind = withNativeWind(config, { input: './global.css' });

module.exports = configWithNativeWind;