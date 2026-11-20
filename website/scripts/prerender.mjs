// Injects the server-rendered (Indonesian) markup into dist/index.html so the
// page has full content without JavaScript, then removes the SSR bundle.
import { readFile, writeFile, rm, access } from 'node:fs/promises'
import { fileURLToPath, pathToFileURL } from 'node:url'
import path from 'node:path'

const root = path.dirname(path.dirname(fileURLToPath(import.meta.url)))
const dist = path.join(root, 'dist')
const ssrDir = path.join(root, 'dist-ssr')

const { render } = await import(pathToFileURL(path.join(ssrDir, 'entry-server.js')).href)
const htmlPath = path.join(dist, 'index.html')
const template = await readFile(htmlPath, 'utf8')
if (!template.includes('<!--app-html-->')) throw new Error('Missing <!--app-html--> placeholder')
const html = template.replace('<!--app-html-->', render())
await writeFile(htmlPath, html)
await rm(ssrDir, { recursive: true, force: true })

// Guard: the account deletion page must ship with every build.
await access(path.join(dist, 'delete-account.html'))
console.log('Prerendered index.html (%d KB) and verified delete-account.html', Math.round(html.length / 1024))
