" For developer of Nova:
" Add those lines into your vimrc file.
" You can do this by "cat nova.vimrc >> ~/.vimrc"

" we use spaces instead of tabs, and an indent is 2 spaces
set softtabstop=2
set expandtab
set smartindent
set shiftwidth=2
set tabstop=2

" show the line numbers
set nu

" turn on syntax highlighting
syntax on

" enable mouse in all modes
set mouse=a

" automatically read file when it is changed from the outside
set autoread

" set up backspace
set backspace=eol,start,indent

