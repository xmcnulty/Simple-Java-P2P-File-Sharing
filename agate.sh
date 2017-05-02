#!/usr/bin/expect

spawn ssh -Y xqe4@agate.cs.unh.edu
expect "password"
send "Tim\[30\]thomas\r"
interact
