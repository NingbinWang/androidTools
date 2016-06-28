#!/bin/bash

echo "========================================="
echo " git_push.sh Script V3.0"
echo "========================================="

up_remote=172.29.0.92

if [ "$#" -eq 0 ];then
	git push
	exit 1
fi

isRepository=false
command="git push"
index=1
for arg in "$@"
do 
	echo "Arg #$index = $arg"
	
	let index+=1
	if [ -z "$(echo $arg | grep "-")" ] && [ $isRepository == false ]; then
		isRepository=true
		if [ -n "$(git remote show | grep $arg)" ];then

		projectname_pattern="remote.$arg.projectname"

		remote_name=$(git config --list | awk -F "=" '{if($1=="'$projectname_pattern'") print $2}')

		remote_url="ssh://$up_remote/$remote_name"

		command="$command $remote_url"		
		else
			echo "fatal: can not found the remote repository '$arg'"
			exit 1;
		fi
	else
		command="$command $arg"
	fi

done
echo "execute command: $command"
$command

exit 0





