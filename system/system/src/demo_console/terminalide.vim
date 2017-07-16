"Setup script for vim build environment
"This requires that you started vim from the src/ folder!

"Need to alter the :make function as a library is used
set makeprg=javac\ -cp\ ../libs/demolib.jar\ -d\ ../build/\ %

"Need to add a library to java complete
call javacomplete#AddClassPath("../libs/demolib.dex.jar")
