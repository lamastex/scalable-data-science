#!/bin/env bash

echo "Dear $1, you recently send me an email concerning $2 and gave me the files:"
ls $3
echo "The attached file contains all you need to know."
echo "Best,"
echo "Prof. XYZ"

#cp /examplefile $4/pleaselookatme

exit 0

