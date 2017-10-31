#!/bin/bash
while true; do ruby -e 'a=STDIN.readlines;10.times do;b=[]; date = Time.now.to_s; b << date; b << Time.new.sec.to_s; 4.times do; b << a[rand(a.size)].chomp end; puts b.join(","); end;' < /usr/share/dict/words > $( date '+%S.log' ); sleep 2; done
