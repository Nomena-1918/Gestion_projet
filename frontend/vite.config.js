import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
    plugins: [vue()],
    base: '/',
    resolve: {
        alias: {
            '@': resolve(__dirname, 'src')
        }
    },
    server: {
        proxy: {
            '/ecran-travail': {
                target: 'http://localhost:8080/gestion_projet_cinema',
                changeOrigin: true,
                secure: false
            },
            '/api': {
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