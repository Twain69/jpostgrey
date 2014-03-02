#!/bin/bash

for x in {1..50}; do
	for i in {1..20}; do 
		echo -e "client_address=10.201.$x.$i\nsender=oxmox@idefix.flegler.com\nrecipient=oxmox@oxmox-nb.flegler.com\n\n" 2>&1 | nc localhost 9989 >/dev/null 2>&1 
		#cat testinput.txt | nc localhost 9989 &
	done
done
