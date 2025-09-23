import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');

// import { Editor } from '@tiptap/vue-3';
// import StarterKit from '@tiptap/starter-kit';
// import Highlight from '@tiptap/extension-highlight';