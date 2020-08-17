module.exports = {
  extends: 'airbnb-base',
  rules: {
    'no-console': 'off',
  },
  overrides: [
    {
      files: ['*.js'],
      excludedFiles: 'node_modules/*',
    },
  ],
};
