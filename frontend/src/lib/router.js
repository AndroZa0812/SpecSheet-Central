import { writable, derived } from 'svelte/store'

export const currentPath = writable(window.location.pathname + window.location.search)

function resolvePath(to) {
  return to.startsWith('/') ? to : '/' + to
}

export function navigate(to) {
  const path = resolvePath(to)
  history.pushState({}, '', path)
  currentPath.set(window.location.pathname + window.location.search)
}

export function link(href) {
  return {
    href: resolvePath(href),
    onclick: (e) => {
      e.preventDefault()
      navigate(href)
    }
  }
}

window.addEventListener('popstate', () => {
  currentPath.set(window.location.pathname + window.location.search)
})

export function match(pattern, path) {
  const patternParts = pattern.split('/')
  const pathParts = path.split('/')
  if (patternParts.length !== pathParts.length) return null

  const params = {}
  for (let i = 0; i < patternParts.length; i++) {
    if (patternParts[i].startsWith(':')) {
      params[patternParts[i].slice(1)] = pathParts[i]
    } else if (patternParts[i] !== pathParts[i]) {
      return null
    }
  }
  return params
}

export function queryParams(search) {
  const params = {}
  if (search?.startsWith('?')) {
    search.slice(1).split('&').forEach(pair => {
      const [k, v] = pair.split('=')
      if (k) params[decodeURIComponent(k)] = decodeURIComponent(v || '')
    })
  }
  return params
}

export const query = derived(currentPath, $p => queryParams($p.split('?')[1]))
