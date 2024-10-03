CODE=0
docker build --tag=html-validator-spider .

docker run -e BASE_URL=https://assets.insiderpie.de/html-spider-validator-1.html html-validator-spider | tee a00.txt
diff e00.txt a00.txt || CODE=1

docker run -e BASE_URL=https://assets.insiderpie.de/html-spider-validator-1.html \
  -e IGNORE_CSS=1 \
  -e TREAT_WARNINGS_AS_ERRORS=1 \
  html-validator-spider | tee a11.txt
diff e11.txt a11.txt || CODE=2

rm a00.txt a11.txt

if [ "$CODE" = "0" ]; then
  echo "E2E: OK"
else
  echo "E2E: Errors"
fi

exit $CODE
