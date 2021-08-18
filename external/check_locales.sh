#! /bin/bash

for file in `ls ../res/locale`
do
        echo `wc ../res/locale/$file`
done
