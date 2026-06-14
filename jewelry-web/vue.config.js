module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true
      },
      '/images': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  },
  lintOnSave: false
}
