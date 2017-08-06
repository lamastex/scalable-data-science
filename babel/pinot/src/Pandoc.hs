{-# LANGUAGE OverloadedStrings #-}
module Pandoc where

import qualified Data.ByteString.Lazy as B hiding (pack)
import qualified Data.ByteString.Lazy.Char8 as B

import qualified Text.Pandoc.Builder as P
import qualified Text.Pandoc.Options as P
import qualified Text.Pandoc.Readers.Markdown as P
import qualified Text.Pandoc.Writers.Markdown as P

import Data.Default (def)

import Control.Lens

import qualified Data.Sequence as S

import Notebook as N
import Utils

import Data.Text as T

fromNotebook :: N.Notebook -> P.Pandoc
fromNotebook nb = P.setTitle title $ P.doc $ foldMap block (nb^.nCommands)
  where title = P.text (T.unpack (nb^.nName))
        block c | c^.cLanguage == "md" =
                  let parsed = P.readMarkdown def (T.unpack (c^.cCommand))
                      P.Pandoc _ bs = either (error . show) id parsed
                  in blocks bs
                | otherwise =
                  let result = maybe mempty id (N.success c)
                  in P.codeBlock (T.unpack (c^.cCommand)) P.<> result

toMarkdown :: P.Pandoc -> B.ByteString
toMarkdown = B.pack . P.writeMarkdown (def { P.writerExtensions = P.githubMarkdownExtensions })
