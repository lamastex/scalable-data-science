docker run --rm -d -it --name=rust-mdbook  --mount type=bind,source=${PWD},destination=/root/book lamastex/rust-mdbook:latest
docker exec -it rust-mdbook /bin/bash
root@c56a8173ca41:~# cd book/
root@c56a8173ca41:~/book# ls
book.toml  scroll-mdbook-outputs.css  src
root@c56a8173ca41:~/book# mdbook build
