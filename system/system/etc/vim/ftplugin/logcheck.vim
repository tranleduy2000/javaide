" Vim filetype plugin file
" Language:    Logcheck
" Maintainer:  Debian Vim Maintainers <pkg-vim-maintainers@lists.alioth.debian.org>
" Last Change: 2008-08-30
" License:     GNU GPL, version 2.0
" URL:         http://git.debian.org/?p=pkg-vim/vim.git;a=blob_plain;f=runtime/ftplugin/logcheck.vim;hb=debian

if exists("b:did_ftplugin")
    finish
endif

let b:did_ftplugin = 1

" Do not hard-wrap lines since logcheck requires one line per regex
setlocal textwidth=0
