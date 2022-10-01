#!/bin/bash

# Author BaeHyeonWoo

if type -p wget
then
    echo "Found wget"
else
    echo "wget was not found on this machine. Please install with your Package Manager. Exiting..."
    exit
fi

if type -p unzip
then
    echo "Found unzip"
else
    echo "unzip was not found on this machine. Please install with your Package Manager. Exiting..."
    exit
fi

if type -p java
then
  echo "Found java"
  # TODO: CHECK JAVA VERSION (8, 11, 16, 17)
else
  echo "Java was not found on this machine. Please install with your Package Manager. Exiting..."
  exit
fi

wget -c --content-disposition -P "./.server" -N "https://github.com/aroxu/server-script/releases/latest/download/server_linux_x64.zip" 2>&1 | tail -1

if [ -d "./.server" ]
then
    cd .server || return
else
    mkdir .server
    cd .server || return
fi

if [ -f "server" ]; then rm -rf ./server; fi

if [ -f "server_linux_x64.zip" ]
then
  unzip server_linux_x64.zip >> /dev/null
  rm -rf ./server_linux_x64.zip
  chmod +x ./server
  ./server
else
  echo "Something went wrong! Try manually download server from: https://github.com/arxou/server-script/releases."
  echo "Exiting..."
  exit
fi

echo "Exiting..."
exit