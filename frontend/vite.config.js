import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    base: '/gestion_projet_cinema',
    server: {
        proxy: {
            '/ecran-travail': {
                target: 'http://localhost:8080/gestion_projet_cinema',
                changeOrigin: true,
                secure: false
            },
            '/gestion_projet_cinema': {
                target: 'http://localhost:8080/gestion_projet_cinema',
                changeOrigin: true,
                rewrite: (path) => path
            }
        }
    },
    build: {
        outDir: '../backend/src/main/resources/static',
        emptyOutDir: true,
        assetsDir: 'assets',
        sourcemap: false,
    }
})