FROM lamastex/haskell-pinot
ENV HOME='/root'
RUN echo "$HOME"
ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
