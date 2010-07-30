all: image_pool_maintainer trash_cleaner

image_pool_maintainer: image_pool_maintainer.c
	gcc image_pool_maintainer.c -o image_pool_maintainer

trash_cleaner: trash_cleaner.cpp    
	g++ trash_cleaner.cpp -o trash_cleaner -lvirt

clean:
	rm -f image_pool_maintainer

